package co.com.kura.b2c.service;

import co.com.kura.b2c.api.dto.*;
import co.com.kura.b2c.domain.entity.*;
import co.com.kura.b2c.domain.repository.*;
import co.com.kura.b2c.infrastructure.PaymentProvider;
import co.com.kura.b2c.infrastructure.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final WalkinTicketRepository walkinTicketRepository;
    private final PaymentRepository paymentRepository;
    private final MasterServiceRepository masterServiceRepository;
    private final LabOfferingRepository labOfferingRepository;
    private final PointOfServiceRepository pointOfServiceRepository;
    private final PaymentProvider paymentProvider;

    private static final String ORDER_NUMBER_PREFIX = "ORD-";
    private static final String TICKET_PREFIX = "WI-";
    private static final int TICKET_CODE_LENGTH = 8;
    private static final int TICKET_VALIDITY_DAYS = 15;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, UUID userId) {
        // Validate single PoS
        PointOfService pos = pointOfServiceRepository.findByIdAndDeletedAtIsNull(request.getPosId())
            .orElseThrow(() -> new IllegalArgumentException("Point of service not found"));

        // Create order
        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .userId(userId)
            .posId(request.getPosId())
            .guestEmail(request.getGuestEmail())
            .guestCedula(request.getGuestCedula())
            .totalAmount(BigDecimal.ZERO)
            .status("PENDING")
            .build();

        Order savedOrder = orderRepository.save(order);

        // Create order items and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            MasterService service = masterServiceRepository
                .findByCodeAndDeletedAtIsNull(itemRequest.getMasterServiceCode())
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + itemRequest.getMasterServiceCode()));

            // Look up price from LabOffering, fallback to basePrice
            BigDecimal price = labOfferingRepository
                .findByPosIdAndMasterServiceIdAndDeletedAtIsNull(request.getPosId(), service.getId())
                .map(LabOffering::getPrice)
                .orElse(service.getBasePrice());

            OrderItem item = OrderItem.builder()
                .orderId(savedOrder.getId())
                .masterServiceId(service.getId())
                .serviceName(service.getName())
                .quantity(itemRequest.getQuantity())
                .unitPrice(price)
                .build();

            orderItemRepository.save(item);
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        // Update order total
        savedOrder.setTotalAmount(totalAmount);
        orderRepository.save(savedOrder);

        // Generate walk-in ticket
        WalkinTicket ticket = WalkinTicket.builder()
            .orderId(savedOrder.getId())
            .ticketCode(generateTicketCode())
            .expiresAt(OffsetDateTime.now().plus(TICKET_VALIDITY_DAYS, ChronoUnit.DAYS))
            .build();

        WalkinTicket savedTicket = walkinTicketRepository.save(ticket);

        // Create mock payment
        PaymentResult paymentResult = paymentProvider.createPayment(
            savedOrder.getId(),
            totalAmount,
            "COP",
            "Order " + savedOrder.getOrderNumber()
        );

        Payment payment = Payment.builder()
            .orderId(savedOrder.getId())
            .provider("MercadoPago")
            .externalId(paymentResult.externalId())
            .checkoutUrl(paymentResult.checkoutUrl())
            .amount(totalAmount)
            .currency("COP")
            .status(paymentResult.status())
            .build();

        Payment savedPayment = paymentRepository.save(payment);

        return buildOrderResponse(savedOrder, savedTicket, savedPayment);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNumber));

        WalkinTicket ticket = walkinTicketRepository.findByOrderId(order.getId()).orElse(null);
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);

        return buildOrderResponse(order, ticket, payment);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(UUID userId) {
        List<Order> orders = orderRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        
        return orders.stream()
            .map(order -> {
                WalkinTicket ticket = walkinTicketRepository.findByOrderId(order.getId()).orElse(null);
                Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
                return buildOrderResponse(order, ticket, payment);
            })
            .collect(Collectors.toList());
    }

    private OrderResponse buildOrderResponse(Order order, WalkinTicket ticket, Payment payment) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        PointOfService pos = pointOfServiceRepository.findById(order.getPosId()).orElse(null);

        return OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .posName(pos != null ? pos.getName() : null)
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus())
            .items(items.stream()
                .map(item -> OrderItemResponse.builder()
                    .id(item.getId())
                    .masterServiceId(item.getMasterServiceId())
                    .serviceName(item.getServiceName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .build())
                .collect(Collectors.toList()))
            .walkinTicket(ticket != null ? WalkinTicketResponse.builder()
                .id(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .expiresAt(ticket.getExpiresAt())
                .redeemedAt(ticket.getRedeemedAt())
                .build() : null)
            .payment(payment != null ? PaymentResponse.builder()
                .id(payment.getId())
                .provider(payment.getProvider())
                .externalId(payment.getExternalId())
                .checkoutUrl(payment.getCheckoutUrl())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .build() : null)
            .createdAt(order.getCreatedAt())
            .build();
    }

    private String generateOrderNumber() {
        return ORDER_NUMBER_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateTicketCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TICKET_PREFIX);
        for (int i = 0; i < TICKET_CODE_LENGTH; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

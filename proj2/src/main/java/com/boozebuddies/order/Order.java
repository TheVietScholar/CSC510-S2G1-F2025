import java.math.BigDecimal;
import java.util.List;

public class Order {

	private Long orderId;
	private OrderStatus status;
	private OrderType type;
	private BigDecimal totalAmount;
	private List<OrderItem> items;
	private ComplianceResult compliance;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public OrderType getType() {
		return type;
	}

	public void setType(OrderType type) {
		this.type = type;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

	public ComplianceResult getCompliance() {
		return compliance;
	}

	public void setCompliance(ComplianceResult compliance) {
		this.compliance = compliance;
	}
}

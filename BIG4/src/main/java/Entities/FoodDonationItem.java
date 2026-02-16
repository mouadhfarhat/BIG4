package Entities;

public class FoodDonationItem {

    private Integer donationEventId;
    private Integer itemId;
    private Integer quantity;

    // For display purposes (joined from dish table)
    private String itemName;


    public FoodDonationItem() {
    }

    /**
     * Constructor with all fields
     * @param donationEventId The donation event ID
     * @param itemId The dish/item ID
     * @param quantity The quantity of this item
     */
    public FoodDonationItem(Integer donationEventId, Integer itemId, Integer quantity) {
        this.donationEventId = donationEventId;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    // ==================== GETTERS AND SETTERS ====================

    public Integer getDonationEventId() {
        return donationEventId;
    }

    public void setDonationEventId(Integer donationEventId) {
        this.donationEventId = donationEventId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    // ==================== TO STRING ====================

    @Override
    public String toString() {
        return "FoodDonationItem{" +
                "donationEventId=" + donationEventId +
                ", itemId=" + itemId +
                ", quantity=" + quantity +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}
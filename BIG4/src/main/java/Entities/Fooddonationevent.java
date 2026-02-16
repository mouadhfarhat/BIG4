package Entities;

import java.sql.Date;
import java.sql.Timestamp;

public class Fooddonationevent {

    private Integer donationEventId;
    private Date eventDate;
    private Integer totalQuantity;
    private String charityName;
    private String status;
    private Long deliveryId;
    private String calendarEventId;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors
    public Fooddonationevent() {
    }

    public Fooddonationevent(Date eventDate, Integer totalQuantity, String charityName) {
        this.eventDate = eventDate;
        this.totalQuantity = totalQuantity;
        this.charityName = charityName;
        this.status = "PENDING";
    }

    public Fooddonationevent(Date eventDate, Integer totalQuantity, String charityName,
                             String status, Long deliveryId, String calendarEventId) {
        this.eventDate = eventDate;
        this.totalQuantity = totalQuantity;
        this.charityName = charityName;
        this.status = status;
        this.deliveryId = deliveryId;
        this.calendarEventId = calendarEventId;
    }

    // Getters and Setters
    public Integer getDonationEventId() {
        return donationEventId;
    }

    public void setDonationEventId(Integer donationEventId) {
        this.donationEventId = donationEventId;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getCharityName() {
        return charityName;
    }

    public void setCharityName(String charityName) {
        this.charityName = charityName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getCalendarEventId() {
        return calendarEventId;
    }

    public void setCalendarEventId(String calendarEventId) {
        this.calendarEventId = calendarEventId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // toString
    @Override
    public String toString() {
        return "FoodDonationEvent{" +
                "donationEventId=" + donationEventId +
                ", eventDate=" + eventDate +
                ", totalQuantity=" + totalQuantity +
                ", charityName='" + charityName + '\'' +
                ", status='" + status + '\'' +
                ", deliveryId=" + deliveryId +
                ", calendarEventId='" + calendarEventId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
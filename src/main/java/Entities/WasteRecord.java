package Entities;

import java.time.LocalDateTime;

public class WasteRecord {

    private Long id;
    private Long ingredientId;
    private double quantityWasted;
    private String wasteType;
    private LocalDateTime date;
    private String reason;

    public WasteRecord(Long id) {
        this.id = id;
    }

    public WasteRecord(Long id, Long ingredientId,
                       double quantityWasted, String wasteType,
                       LocalDateTime date, String reason) {
        this.id = id;
        this.ingredientId = ingredientId;
        this.quantityWasted = quantityWasted;
        this.wasteType = wasteType;
        this.date = date;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(Long ingredientId) {
        this.ingredientId = ingredientId;
    }

    public double getQuantityWasted() {
        return quantityWasted;
    }

    public void setQuantityWasted(double quantityWasted) {
        this.quantityWasted = quantityWasted;
    }

    public String getWasteType() {
        return wasteType;
    }

    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "WasteRecord{" +
                "id=" + id +
                ", ingredientId=" + ingredientId +
                ", quantityWasted=" + quantityWasted +
                ", wasteType='" + wasteType + '\'' +
                ", date=" + date +
                ", reason='" + reason + '\'' +
                '}';
    }
}
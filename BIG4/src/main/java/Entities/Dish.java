package Entities;
import java.sql.Timestamp;
public class Dish {
    private int id ;
    private int menu_id ;
    private String name ;
    private String description;
    private float base_price ;
    private Boolean available;
    private int stock_quantity ;
    private String image_url ;
    private Timestamp created_at;
    private Timestamp update_at;
    public Dish() {}

    public Dish(int id, int menu_id, String name, String description, float base_price, Boolean available, int stock_quantity, Timestamp created_at, String image_url, Timestamp update_at) {
        this.id = id;
        this.menu_id = menu_id;
        this.name = name;
        this.description = description;
        this.base_price = base_price;
        this.available = available;
        this.stock_quantity = stock_quantity;
        this.created_at = created_at;
        this.image_url = image_url;
        this.update_at = update_at;
    }

    public Dish(String name, String description, float base_price, Boolean available, int stock_quantity, Timestamp created_at, String image_url, Timestamp update_at) {
        this.name = name;
        this.description = description;
        this.base_price = base_price;
        this.available = available;
        this.stock_quantity = stock_quantity;
        this.created_at = created_at;
        this.image_url = image_url;
        this.update_at = update_at;
    }

    public int getId() {
        return id;
    }
    public int getMenu_id() {
        return menu_id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float getBase_price() {
        return base_price;
    }

    public Boolean getAvailable() {
        return available;
    }

    public int getStock_quantity() {
        return stock_quantity;
    }

    public String getImage_url() {
        return image_url;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public Timestamp getUpdate_at() {
        return update_at;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setMenu_id(int id) {
        this.menu_id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBase_price(float base_price) {
        this.base_price = base_price;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public void setStock_quantity(int stock_quantity) {
        this.stock_quantity = stock_quantity;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public void setUpdate_at(Timestamp update_at) {
        this.update_at = update_at;
    }

    @Override
    public String toString() {
        return "Dish{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", base_price=" + base_price +
                ", available=" + available +
                ", stock_quantity=" + stock_quantity +
                ", image_url='" + image_url + '\'' +
                ", created_at=" + created_at +
                ", update_at=" + update_at +
                '}';
    }
}

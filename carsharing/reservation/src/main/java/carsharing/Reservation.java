package carsharing;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;


@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long carId;
    private Long userId;
    private String usage;
    private String status;

    @PostPersist
    public void onPostPersist(){
        CarRented carRented = new CarRented();
        carRented.setCarId(this.carId);
        carRented.setStatus("using");
        carRented.setUserId(this.userId);
        BeanUtils.copyProperties(this, carRented);
        carRented.publishAfterCommit();

        carsharing.external.Car car = new carsharing.external.Car();
        // mappings goes here
        // car.setStatus("using");
        ReservationApplication.applicationContext.getBean(carsharing.external.CarService.class)
            .changeStatus(this.carId);
    }
    
    @PostUpdate
    public void onPostUpdate() {
        final CarReturned carReturned = new CarReturned();
        carReturned.setCarId(this.carId);
        carReturned.setStatus("availble");
        carReturned.setUsage(this.usage);
        carReturned.setUserId(this.userId);
        BeanUtils.copyProperties(this, carReturned);
        carReturned.publishAfterCommit();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
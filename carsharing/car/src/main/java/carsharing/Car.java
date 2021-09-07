package carsharing;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="Car_table")
public class Car {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    // private Long carId;
    private String status;
    private String expense;
    private Long userId;

    @PostPersist
    public void onPostPersist(){
        
        CarRegistered carRegistered = new CarRegistered();
        // carRegistered.setCarId(this.carId);
        carRegistered.setStatus("available");
        BeanUtils.copyProperties(this, carRegistered);
        carRegistered.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate() {
         ////////////////////////////////
        // RESERVATION에 UPDATE 된 경우
        ////////////////////////////////
        if(this.getStatus().equals("using")) {

            ///////////////////////
            // 렌트 요청 들어온 경우
            ///////////////////////

            // 이벤트 발생 --> StatusChanged
            StatusChanged statusChanged = new StatusChanged();
            // statusChanged.setCarId(this.getCarId());
            statusChanged.setStatus("using");
            BeanUtils.copyProperties(this, statusChanged);
            statusChanged.publishAfterCommit();
        }
      
        if(this.getStatus().equals("available")) {

            ///////////////////////
            // 렌트 반납 들어온 경우
            ///////////////////////

            // 이벤트 발생 --> ExpenseCalculated
            ExpenseCalculated expenseCalculated = new ExpenseCalculated();
            // expenseCalculated.setCarId(this.carId);
            expenseCalculated.setExpense("100000");
            BeanUtils.copyProperties(this, expenseCalculated);
            expenseCalculated.publishAfterCommit();
        }
        
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    // public Long getCarId() {
    //     return carId;
    // }

    // public void setCarId(Long carId) {
    //     this.carId = carId;
    // }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getExpense() {
        return expense;
    }

    public void setExpense(String expense) {
        this.expense = expense;
    }
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
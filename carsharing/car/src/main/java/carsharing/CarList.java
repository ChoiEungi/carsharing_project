package carsharing;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="CarList_table")
public class CarList {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private Long carId;
        private String status;


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
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

}

package carsharing;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarListRepository extends CrudRepository<CarList, Long> {

    List<CarList> findByCarId(Long carId);
    

}
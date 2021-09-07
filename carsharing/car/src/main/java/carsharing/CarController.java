package carsharing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;

 @RestController
 public class CarController {
    @Autowired
    CarRepository carRepository;

    @RequestMapping(value = "/cars/changeStatus",
                    method = RequestMethod.PUT,
                    produces = "application/json;charset=UTF-8")
    public boolean changeStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
            
            // try {
            //     Thread.currentThread().sleep((long) (400 + Math.random() * 220));
            // } catch (InterruptedException e) {
            //     e.printStackTrace();
            // }
            long id = Long.valueOf(request.getParameter("carId"));
            System.out.println("######################## changeStatus id : " + id);


            Optional<Car> res = carRepository.findById(id);
            Car car = res.get();

            car.setStatus("using");
            // // DB Update
            carRepository.save(car);

            boolean result = true;
            return result;
            
    }

 }
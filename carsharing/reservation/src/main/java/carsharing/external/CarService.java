package carsharing.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@FeignClient(name="car", url="${api.url.car}")
public interface CarService {
    @RequestMapping(method= RequestMethod.PUT, path="/cars/changeStatus")
    public void changeStatus(@RequestParam("carId") long carId);

}


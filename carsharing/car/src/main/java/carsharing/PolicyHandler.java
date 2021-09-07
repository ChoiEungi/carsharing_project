package carsharing;

import carsharing.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired CarRepository carRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCarReturned_ConfirmReturn(@Payload CarReturned carReturned){

        // if(!carReturned.validate()) return;

        System.out.println("\n\n##### listener ConfirmReturn : " + carReturned.toJson() + "\n\n");
        if(!carReturned.validate()) {
            /////////////////////////////////////////////
            // 반납 요청이 왔을 때 -> status -> available
            /////////////////////////////////////////////
            System.out.println("##### listener vaccineRegistered : " + carReturned.toJson());
            Car car = new Car();

            car.setId(carReturned.getId());
            car.setStatus("available");
            
            // DB Update
            carRepository.save(car);
        }

            

    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}

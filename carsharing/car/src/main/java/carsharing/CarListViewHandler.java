package carsharing;

import carsharing.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class CarListViewHandler {


    @Autowired
    private CarListRepository carListRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenCarRegistered_then_CREATE_1 (@Payload CarRegistered carRegistered) {
        try {

            if (!carRegistered.validate()) return;

            // view 객체 생성
            CarList carList = new CarList();
            // view 객체에 이벤트의 Value 를 set 함
            carList.setCarId(carRegistered.getCarId());
            carList.setStatus(carRegistered.getStatus());
            // view 레파지 토리에 save
            carListRepository.save(carList);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenStatusChanged_then_UPDATE_1(@Payload StatusChanged statusChanged) {
        try {
            if (!statusChanged.validate()) return;
                // view 객체 조회

                    List<CarList> carListList = carListRepository.findByCarId(statusChanged.getCarId());
                    for(CarList carList : carListList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    carList.setCarId(statusChanged.getCarId());
                    carList.setStatus("using");
                // view 레파지 토리에 save
                carListRepository.save(carList);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenExpenseCalculated_then_UPDATE_2(@Payload ExpenseCalculated expenseCalculated) {
        try {
            if (!expenseCalculated.validate()) return;
                // view 객체 조회

                    List<CarList> carListList = carListRepository.findByCarId(expenseCalculated.getCarId());
                    for(CarList carList : carListList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    carList.setCarId(expenseCalculated.getCarId());
                    carList.setStatus("available");
                // view 레파지 토리에 save
                carListRepository.save(carList);
                }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}


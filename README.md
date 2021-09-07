![image](https://user-images.githubusercontent.com/50560622/132305061-dd81573d-cf64-43b4-a0ef-8844d8c6d563.png)


# car sharing

본 예제는 MSA/DDD/Event Storming/EDA 를 포괄하는 분석/설계/구현/운영 전단계를 커버하도록 구성한 예제입니다.
이는 클라우드 네이티브 애플리케이션의 개발에 요구되는 체크포인트들을 통과하기 위한 예시 답안을 포함합니다.
- 체크포인트 : https://workflowy.com/s/assessment-check-po/T5YrzcMewfo4J6LW


# Table of contents

- [예제 - carsharing](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [CQRS](##CQRS)  
    - [API Gateway](##API-게이트웨이)
    - [Correlation](##Correlation)
    - [DDD 의 적용](##DDD-의-적용)
    - [폴리글랏 퍼시스턴스](##폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)


# 서비스 시나리오

carsharing 커버하기

기능적 요구사항
1. 호스트가 대여 차량을 등록한다.
2. 고객이 차량을 선택하여 렌트한다.
3. 렌트와 동시에 다른 사람이 동시에 이용할 수 없도록 차량 상태가 변경된다.
4. 고객이 차량이용이 끝나면 차량을 반납한다.
5. 호스트가 차량 반납을 확정하고 요금을 계산한다.
6. 요금이 계산된 후 결제가 진행된다.
7. 차량 이용 상태를 한 화면에서 확인 할 수 있다.(viewpage)

비기능적 요구사항
1. 트랜잭션
    1. 차량 상태가 변경되지 않은 건은 예약이 되면 안된다. (Sync 호출)
1. 장애격리
    1. 결제 및 차량 서비스가 되지 않더라도 이용이 끝나면 반납처리를 할 수 있어야한다.  Async (event-driven), Eventual Consistency
    1. 예약 시스템이 과중되면 사용자를 잠시동안 받지 않고 잠시 후에 하도록 유도한다  Circuit breaker, fallback
1. 성능
    1. 차량 상태 등을 한번에 확인할 수 있어야 한다  (CQRS)


# 체크포인트

- 분석 설계

  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?


# 분석/설계

## 완성 모형 및 기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/50560622/132271847-5e75b6da-8e04-46c4-b33a-108b0a48af41.png)


    - 호스트가 대여 차량을 등록한다. (ok)
    - 고객이 차량을 선택하여 렌트한다. (ok)
    - 렌트와 동시에 다른 사람이 동시에 이용할 수 없도록 차량 상태가 변경된다. (ok)
    - 고객이 차량이용이 끝나면 차량을 반납한다. (ok)
    - 호스트가 차량 반납을 확정하고 요금을 계산한다. (ok)
    - 요금이 계산된 후 결제가 진행된다. (ok)
    - 차량 이용 상태를 한 화면에서 확인 할 수 있다.(viewpage) (ok)


## 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/50560622/132272059-dca846fc-6abf-4ba2-afe8-0d7be4a5fd88.png)

    1. 차량 상태가 변경되지 않은 건은 예약이 되면 안된다. (Sync 호출)
    2. 결제 및 차량 서비스가 되지 않더라도 이용이 끝나면 반납처리를 할 수 있어야한다.  Async (event-driven), Eventual Consistency
    3. 예약 시스템이 과중되면 사용자를 잠시동안 받지 않고 잠시 후에 하도록 유도한다  Circuit breaker, fallback
    4. 차량 상태 등을 한번에 확인할 수 있어야 한다  (CQRS)
    
## 헥사고날 아키텍처 다이어그램 도출

![image](https://user-images.githubusercontent.com/50560622/132272287-c94bad9c-00e5-4d07-9c96-874012a1a616.png)

    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 과 Req/Resp 를 구분함
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현:

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
   mvn spring-boot:run
```

## CQRS

차량 별로 Status 에 대하여 고객(Customer)이 조회 할 수 있도록 CQRS 로 구현하였다.
- reservation, car, payment 개별 Aggregate Status 를 통합 조회하여 성능 Issue 를 사전에 예방할 수 있다.
- 비동기식으로 처리되어 발행된 이벤트 기반 Kafka 를 통해 수신/처리 되어 별도 Table 에 관리한다
- Table 모델링 (carList)  
  ![image](https://user-images.githubusercontent.com/50560622/132272567-47b3492a-4cc3-4f27-a7c2-c463b54826c1.png)
- viewpage MSA ViewHandler 를 통해 구현 ("CarRegistered" 이벤트 발생 시, Pub/Sub 기반으로 별도 carList 테이블에 저장)
  ![image](https://user-images.githubusercontent.com/50560622/132285852-022b5d6c-fee6-4564-9acd-0f9faed68743.png)
  ![image](https://user-images.githubusercontent.com/50560622/132285923-6678d219-5344-443b-8487-13c32ebc1e6d.png)

- 실제로 view 페이지를 조회해 보면 모든 car 별로 상태를 조회할 수 있다.
  ![image](https://user-images.githubusercontent.com/50560622/132286003-1fb91cb6-d961-4279-93dd-f431bc825923.png)



## API 게이트웨이
      1. gateway 스프링부트 App을 추가 후 application.yaml내에 각 마이크로 서비스의 routes 를 추가하고 gateway 서버의 포트를 8080 으로 설정함
       
          - application.yaml 예시
            ```
		spring:
		  profiles: docker
		  cloud:
		    gateway:
		      routes:
			- id: reservation
			  uri: http://reservation:8080
			  predicates:
			    - Path=/reservations/** 
			- id: car
			  uri: http://car:8080
			  predicates:
			    - Path=/cars/** /carLists/**
			- id: payment
			  uri: http://payment:8080
			  predicates:
			    - Path=/payments/** 
		      globalcors:
			corsConfigurations:
			  '[/**]':
			    allowedOrigins:
			      - "*"
			    allowedMethods:
			      - "*"
			    allowedHeaders:
			      - "*"
			    allowCredentials: true

		server:
		  port: 8080          
            ```

         
      2. Kubernetes용 Deployment.yaml 을 작성하고 Kubernetes에 Deploy를 생성함
          - Deployment.yaml 예시
          

            ```
		apiVersion: apps/v1
		kind: Deployment
		metadata:
		  name: gateway
		  labels:
		    app: gateway
		spec:
		  replicas: 1
		  selector:
		    matchLabels:
		      app: gateway
		  template:
		    metadata:
		      labels:
			app: gateway
		    spec:
		      containers:
			- name: gateway
			  image: user10.azurecr.io/gateway:latest
			  ports:
			    - containerPort: 8080
            ```               
            

            ```
            Deploy 생성
            kubectl apply -f deployment.yaml
            ```     
          - Kubernetes에 생성된 Deploy. 확인
            
	    
            
      3. Kubernetes용 Service.yaml을 작성하고 Kubernetes에 Service/LoadBalancer을 생성하여 Gateway 엔드포인트를 확인함. 
          - Service.yaml 예시
          
            ```
		apiVersion: v1
		kind: Service
		metadata:
		  name: gateway
		  labels:
		    app: gateway
		spec:
		  ports:
		    - port: 8080
		      targetPort: 8080
		  selector:
		    app: gateway
		  type:
		    LoadBalancer
            ```             

           
            ```
            Service 생성
            kubectl apply -f service.yaml            
            ```             
            
            
          - API Gateay 엔드포인트 확인
           
            ```
            Service  및 엔드포인트 확인 
            kubectl get svc         
            ```                 
![image](https://user-images.githubusercontent.com/50560622/132291282-0de65322-25c6-453f-8a6d-e60b49965684.png)
  

## Correlation

PolicyHandler에서 처리 시 어떤 건에 대한 처리인지를 구별하기 위한 Correlation-key 구현을 
이벤트 클래스 안의 변수로 전달받아 서비스간 연관된 처리를 정확하게 구현하고 있습니다. 

아래의 구현 예제를 보면

차량을 렌트하면 차량관리 에서 차량의 상태가 변경되고, 차량을 반납하면 다시 차량의 상태가 업데이트 되는 것을 확인 할 수 있다.
  
차량 등록  
![image](https://user-images.githubusercontent.com/50560622/132286171-ecaf2a02-4d61-4480-b8e4-5c83b904d160.png)
  
차량 렌트  
![image](https://user-images.githubusercontent.com/50560622/132286245-f6012c78-1fd6-465b-95e4-4d01db731536.png)
  
차량 반납  
![image](https://user-images.githubusercontent.com/50560622/132286324-076fb041-13f0-4cbe-9331-44433f6d48aa.png)


## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다. (예시는 room 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하려고 노력했다. 현실에서 발생가는한 이벤트에 의하여 마이크로 서비스들이 상호 작용하기 좋은 모델링으로 구현을 하였다.

```
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
```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package carsharing;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="cars", path="cars")
public interface CarRepository extends PagingAndSortingRepository<Car, Long>{

}
```
- 적용 후 REST API 의 테스트
```
- 차량 서비스의 차량 등록
http POST http://localhost:8088/cars id=1 status=available

- reservation 서비스의 차량 렌트
http POST http://localhost:8088/reservations carId=1 userId=1 

- reservation 서비스의 차량 반납
http PATCH http://localhost:8088/reservations/1 carId=1 usage=100KM 

```
## 폴리글랏 퍼시스턴스

별다른 작업없이 기존의 Entity Pattern 과 Repository Pattern 적용과 데이터베이스 제품의 설정 (pom.xml) 만으로 hsqldb 로 부착시켰다
```
- pom.xml - in payment 인스턴스

		<dependency>
			<groupId>org.hsqldb</groupId>
			<artifactId>hsqldb</artifactId>
			<scope>runtime</scope>
		</dependency>
```

  ![image](https://user-images.githubusercontent.com/50560622/132288500-32d26e5e-8e07-4445-9a11-af639bc506c4.png)

## 동기식 호출(Sync) 과 Fallback 처리

분석 단계에서의 조건 중 하나로 렌트 시 차량서비스 간의 차량 상태 변경 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다.

- 차량 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
- CarService.java

package carsharing.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@FeignClient(name="car", url="http://localhost:8088")
public interface CarService {
    @RequestMapping(method= RequestMethod.PUT, path="/cars/changeStatus")
    public void changeStatus(@RequestParam("carId") long carId);

}

```

- 차량 렌트를 받은 직후(@PostPersist) 차량 상태 변경을 동기(Sync)로 요청하도록 처리
```
# Reservation.java (Entity)

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
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 차량 서비스가 장애가 나면 렌트를 못한다는 것을 확인:


```
# 차량 (car) 서비스를 잠시 내려놓음 (ctrl+c)

# 차량 렌트
http POST http://localhost:8088/reservations carId=1 userId=1 #Fail
```
  ![image](https://user-images.githubusercontent.com/50560622/132287223-25f2d38d-305b-44a0-b176-fa6b822fb7dd.png)
```
# 차량 서비스 재기동
cd car
mvn spring-boot:run

# 차량 렌트
http POST http://localhost:8088/reservations carId=1 userId=1   #Success
```

- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

차량 반납이 이루어진 후에 차량 서비스에서 차량 반납을 확정짓고, 주행 요금을 계산하며, 요금이 정산된 후에는 결제 시스템에 비동기식으로 처리한다.
 
- 이를 위하여 반납이 요청이 오면 반납 요청되었다는 이벤트를 Kafka 에 송신한다. (Publish)
 
```
- Reservation.java

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
```

- 차량 시스템에서는 차량 반납 요청 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다:

```
- PolicyHandler.java
	
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

```

차량 서비스가 유지보수로 인해 잠시 내려간 상태더라도 차량을 반납 신청하는데는 문제가 없다.
```
- 차량 서비스 (car) 를 잠시 내려놓음 (ctrl+c)

- 차량 반납 요청
http PATCH http://localhost:8088/reservations/1 usage=100KM #Success
![image](https://user-images.githubusercontent.com/50560622/132287982-a089738c-8c87-4313-9907-ddfc315154ec.png)

```

# 운영

## CI/CD 설정


각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 Azure를 사용하였음.


- 도커 이미지

![image](https://user-images.githubusercontent.com/50560622/132291359-c1645dc2-7552-4eb4-b8e3-665ce0558bb7.png)
  
  
- Azure Portal > 컨테이너 레지스트리에 이미지 저장 확인  
![image](https://user-images.githubusercontent.com/50560622/132291417-00daef6f-fd13-4752-a1a3-ac53920431bc.png)
  

  
* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 백신예약 요청--> 예약관리 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml
feign:
  hystrix:
    enabled: true
    
hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```

- 피호출 서비스인 차량(car) 서비스의 임의 부하 처리 - 400 밀리에서 증감 220 밀리 정도 왔다갔다 하게
```
# car.java 

    @RestController
    public class CarController {
    @Autowired
    CarRepository carRepository;

    @RequestMapping(value = "/cars/changeStatus",
                    method = RequestMethod.PUT,
                    produces = "application/json;charset=UTF-8")
    public boolean changeStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
            
            try {
                Thread.currentThread().sleep((long) (400 + Math.random() * 220));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 10명
- 60초 동안 실시

```
$ siege -c10 -t60S --content-type "application/json" 'http://20.200.206.77:8080/reservations POST {"carId":"1"}'

```
![image](https://user-images.githubusercontent.com/50560622/132294985-ccc6afdb-33ff-4766-a468-1f91d5ff1929.png)  


- 운영시스템은 죽지 않고 지속적으로 CB 에 의하여 적절히 회로가 열림과 닫힘이 벌어지면서 자원을 보호하고 있음을 보여줌. 하지만, 61.74% 가 성공한 것은 고객 사용성에 있어 좋지 않기 때문에 Retry 설정과 동적 Scale out (replica의 자동적 추가,HPA) 을 통하여 시스템을 확장 해주는 후속처리가 필요.


## 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 


- 차량서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:
```
kubectl autoscale deploy reservation --min=1 --max=10 --cpu-percent=15
```
- CB 에서 했던 방식대로 워크로드를 1분 동안 걸어준다.
```
$ siege -c10 -t60S --content-type "application/json" 'http://20.200.206.77:8080/reservations POST {"carId":"1"}'

```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy reservation -w

```
- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다:  
![image](https://user-images.githubusercontent.com/50560622/132295395-8755d623-0f66-4b62-99f3-c49c50188264.png)

- 실제로 auto scale 을 걸었을 때 응답율이 82.42%로 향상됨을 알 수 있다.
![image](https://user-images.githubusercontent.com/50560622/132295529-563a6713-4545-446d-96a5-325424ee3f72.png) 


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함

```
kubectl delete deploy/reservation
kubectl delete hpa reservation
```

- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
siege -c30 -t60s --content-type "application/json" 'http://car:8080/cars POST {"status":"available"}'
** SIEGE 4.0.4
** Preparing 30 concurrent users for battle.
The server is now under siege...
Lifting the server siege...
Transactions:                  39487 hits
Availability:                 100.00 %
Elapsed time:                  59.89 secs
Data transferred:               8.17 MB
Response time:                  0.05 secs
Transaction rate:             659.33 trans/sec
Throughput:                     0.14 MB/sec
Concurrency:                   29.90
Successful transactions:       39487
Failed transactions:               0
Longest transaction:            0.50
Shortest transaction:           0.00

```

- 차량 서비스 재시작 후 부하 
```
kubectl set image deployment/car car=user10.azurecr.io/car:v1
```

- seige 의 화면으로 넘어가서 Availability 가 100% 미만으로 떨어졌는지 확인

```
siege -c30 -t60s --content-type "application/json" 'http://car:8080/cars POST {"status":"available"}'

Transactions:                      0 hits
Availability:                   0.00 %
Elapsed time:                   0.30 secs
Data transferred:               0.00 MB
Response time:                  0.00 secs
Transaction rate:               0.00 trans/sec
Throughput:                     0.00 MB/sec
Concurrency:                    0.00
Successful transactions:           0
Failed transactions:            1053
Longest transaction:            0.00
Shortest transaction:           0.00

```
- 배포기간중 Availability 가 평소 100%에서 0% 대로 떨어지는 것을 확인. 원인은 쿠버네티스가 성급하게 새로 올려진 서비스를 READY 상태로 인식하여 서비스 유입을 진행한 것이기 때문. 이를 막기위해 Readiness Probe 를 설정함

```
# deployment.yaml 의 readiness probe 의 설정:
	    readinessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 30
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
            successThreshold: 1
```

```
kubectl apply -f kubernetes/deployment.yml
```

- 동일한 시나리오로 재배포 한 후 Availability 확인:
```
Transactions:                  16806 hits
Availability:                 100.00 %
Elapsed time:                  59.22 secs
Data transferred:               3.47 MB
Response time:                  0.11 secs
Transaction rate:             283.79 trans/sec
Throughput:                     0.06 MB/sec
Concurrency:                   29.92
Successful transactions:       16806
Failed transactions:               0
Longest transaction:            1.67
Shortest transaction:           0.00

```

배포기간 동안 Availability 가 변화없기 때문에 무정지 재배포가 성공한 것으로 확인됨.


# Self-healing (Liveness Probe)
- car deployment.yml 파일 수정 
```
콘테이너 실행 후 /tmp/healthy 파일을 만들고 
90초 후 삭제
livenessProbe에 'cat /tmp/healthy'으로 검증하도록 함
```
![deployment yml tmp healthy](https://user-images.githubusercontent.com/38099203/119318677-8ff0f300-bcb4-11eb-950a-e3c15feed325.PNG)

```
컨테이너 실행 후 90초 동인은 정상이나 이후 /tmp/healthy 파일이 삭제되어 livenessProbe에서 실패를 리턴하게 됨
kubectl describe pod/car-695c6b96d4-zrfql 

Events:
  Type     Reason     Age                 From                                        Message
  ----     ------     ----                ----                                        -------
  Normal   Scheduled  <unknown>                                                       Successfully assigned default/car-695c6b96d4-zrfql to aks-agentpool-34768182-vmss000001
  Normal   Pulled     36m                 kubelet, aks-agentpool-34768182-vmss000001  Successfully pulled image "user10.azurecr.io/car:latest" in 336.692819ms
  Normal   Pulled     35m                 kubelet, aks-agentpool-34768182-vmss000001  Successfully pulled image "user10.azurecr.io/car:latest" in 452.215408ms
  Normal   Created    34m (x3 over 36m)   kubelet, aks-agentpool-34768182-vmss000001  Created container car
  Normal   Started    34m (x3 over 36m)   kubelet, aks-agentpool-34768182-vmss000001  Started container car
  Normal   Killing    34m (x2 over 35m)   kubelet, aks-agentpool-34768182-vmss000001  Container car failed liveness probe, will be restarted
  Normal   Pulling    34m (x3 over 36m)   kubelet, aks-agentpool-34768182-vmss000001  Pulling image "user10.azurecr.io/car:latest"
  Normal   Pulled     34m                 kubelet, aks-agentpool-34768182-vmss000001  Successfully pulled image "user10.azurecr.io/car:latest" in 279.741625ms
  Warning  Unhealthy  31m (x16 over 35m)  kubelet, aks-agentpool-34768182-vmss000001  Liveness probe failed: cat: can't open '/tmp/healthy': No such file or directory
  
pod 정상 상태 일때 pod 진입하여 /tmp/healthy 파일 생성해주면 정상 상태 유지됨
```

![image](https://user-images.githubusercontent.com/50560622/132302066-a1590c4e-bb30-421e-a59f-25e2b36610ee.png)
![image](https://user-images.githubusercontent.com/50560622/132302367-e7673f8c-a1aa-40e6-acb6-d07e6ed0d79f.png)


## Config Map/ Persistence Volume
- Persistence Volume
1. persist volume claim 생성
- pvc.yaml
```
	apiVersion: v1
	kind: PersistentVolumeClaim
	metadata:
	  name: reservation-pvc
	  labels:
	    app: reservation-pvc
	spec:
	  accessModes:
	  - ReadWriteMany
	  resources:
	    requests:
	      storage: 2Ki
	  storageClassName: azurefile
```
```
kubectl apply -f pvc.yaml
```


2. 예약 서비스 Deployment 에 적용
```
apiVersion: apps/v1
kind: Deployment
metadata:
  name: reservation
  labels:
    app: reservation
spec:
  replicas: 1
  selector:
    matchLabels:
      app: reservation
  template:
    metadata:
      labels:
        app: reservation
    spec:
      containers:
        - name: reservation
          image: user10.azurecr.io/reservation:latest
          ports:
            - containerPort: 8080
          envFrom: 
            - configMapRef:
                name: reservation
          resources:
            requests:
              memory: 512Mi
              cpu: 500m
            limits:
              memory: 512Mi
              cpu: 500m
          volumeMounts:
            - name: volume
              mountPath: "/apps/data"
      volumes:
        - name: volume
          persistentVolumeClaim:
            claimName: reservation-pvc
```



3. 예약서비스 A pod에서 파일을 올리고 B pod 에서 확인
```
NAME                         READY   STATUS    RESTARTS   AGE
car-695c6b96d4-zrfql         1/1     Running   7          18m
gateway-85757c9c7-b6mtf      1/1     Running   0          103m
payment-85ff74ffd5-h88w7     1/1     Running   0          94m
reservation-df84994f-mvccs   1/1     Running   0          47s
reservation-df84994f-pmj59   1/1     Running   0          44s


kubectl exec -it reservation-df84994f-mvccs /bin/sh
/ # cd /apps/data
/apps/data # touch testfile
```
- 나머지 Pod 에 접속하여 testfile 유무 확인
```
kubectl exec -it reservation-df84994f-pmj59 /bin/sh/ 
/ # cd apps/data
/apps/data # ls -al

total 4
drwxrwxrwx    2 root     root             0 Sep  7 05:51 .
drwxr-xr-x    3 root     root          4096 Sep  7 07:32 ..
-rwxrwxrwx    1 root     root             0 Sep  7 07:34 testfile
```
![image](https://user-images.githubusercontent.com/50560622/132303829-e90aeefc-2004-47cf-b201-96b9ce9b7421.png)


- Config Map
- Reservation 서비스에서 차량(Car) 서비스 호출 하는 주소를 변수처리하여 컨테이너 런타임 환경에서 Configmap 을 통해 해당 값을 받아서 호출하도록 처리
1: cofingmap.yml 파일 생성
```
kubectl apply -f cofingmap.yml


apiVersion: v1
kind: ConfigMap
metadata:
  name: reservation
data:
  API_URL_CAR: "http://car:8080"
```

2. deployment.yml에 적용하기

```
kubectl apply -f deployment.yml


.......
          envFrom: 
            - configMapRef:
                name: reservation
```

3. pod 에서 변수 세팅되었는지 확인
```
kubectl exec -it reservation-df84994f-pmj59 /bin/sh
/ # env
...
API_URL_CAR=http://car:8080
...
```

![image](https://user-images.githubusercontent.com/50560622/132304219-72402e92-27a6-4267-a414-69f362fe94de.png)


package carsharing;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="cars", path="cars")
public interface CarRepository extends PagingAndSortingRepository<Car, Long>{


}

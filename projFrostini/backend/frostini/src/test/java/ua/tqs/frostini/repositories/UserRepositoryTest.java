package ua.tqs.frostini.repositories;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ua.tqs.frostini.models.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Testcontainers
 class UserRepositoryTest {
  @Container
   static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11.12")
    .withUsername("demo")
    .withPassword("demopw")
    .withDatabaseName("shop");
  
  
  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", container::getJdbcUrl);
    registry.add("spring.datasource.password", container::getPassword);
    registry.add("spring.datasource.username", container::getUsername);
  }
  
  @AfterEach
   void tearDown() {
    userRepository.deleteAll();
    testEntityManager.clear();
    
  }
  
  @Autowired
  private UserRepository userRepository;
  
  
  @Autowired
  private TestEntityManager testEntityManager;
  
  
  @Test
   void whenUserSaved_findByCorrectEmailShouldReturnCorrectUserEntity() {
    User u1 = createAndSaveUser( 1l );
    User u2 = createAndSaveUser( 2l );
    
    Optional<User> optionalUserFromDB = userRepository.findByEmail( u1.getEmail() );
    assertThat( optionalUserFromDB.isPresent() ).isTrue();
    
    User userFromDB = optionalUserFromDB.get();
    assertThat( userFromDB.getEmail() ).isEqualTo( u1.getEmail() );
    assertThat( userFromDB.getName() ).isEqualTo( u1.getName() );
    assertThat( userFromDB.getPwd() ).isEqualTo( u1.getPwd() );
  }
  
  @Test
   void whenUserSaved_findByIncorrectEmailShouldReturnEmptyOptionalObject() {
    User u1 = createAndSaveUser( 1l );
    User u2 = createAndSaveUser( 2l );
    
    Optional<User> optionalUserFromDB = userRepository.findByEmail( "thisisnotavalid@email.com" );
    assertThat( optionalUserFromDB.isPresent() ).isFalse();
    
  }
  
  
  /* -- helper -- */
  private User createAndSaveUser( long i ) {
    User u = new User();
    u.setName( "Pedro" );
    u.setPwd( "safepassword" );
    u.setEmail( "pdfl" + i + "@ua.pt" );
    testEntityManager.persistAndFlush( u );
    return u;
  }
}

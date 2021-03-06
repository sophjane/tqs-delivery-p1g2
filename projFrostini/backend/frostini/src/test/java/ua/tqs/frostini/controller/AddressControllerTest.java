package ua.tqs.frostini.controller;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import ua.tqs.frostini.datamodels.AddressDTO;
import ua.tqs.frostini.models.Address;
import ua.tqs.frostini.models.User;
import ua.tqs.frostini.service.AddressService;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.hamcrest.Matchers.equalTo;

@WebMvcTest(value = AddressController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AddressControllerTest {
    
  @Autowired
  private MockMvc mvc;

  @MockBean
  private AddressService addressService;

  private User user = createUser(1l);

  AddressDTO addressDTO;

  @BeforeEach
  void setUp() throws IOException {
    RestAssuredMockMvc.mockMvc( mvc );
    addressDTO = createAddressDTO( 1 );
  }

  @Test
  void testPostNewAddressWithValidUser_ThenReturnAddressId() {
    Address a = createAddress( 1 );
    AddressDTO aDto = createAddressDTO( 1 );
    when( addressService.getAddress( any() ) ).thenReturn( a );

    given().contentType( ContentType.JSON ).body( aDto )
                      .when().post( "api/v1/addresses" ).then()
                      .contentType( ContentType.JSON )
                      .status( HttpStatus.OK ).and()
                      .body("latitude", equalTo( (float) a.getLatitude() ) )
                      .body("longitude", equalTo( (float) a.getLongitude() ) );

    verify( addressService, times( 1 ) ).getAddress( any() );
  }

  @Test
  void testPostOldAddressWithValidUser_ThenReturnAddressId() {
    Address a = createAddress( 1 );
    AddressDTO aDto = createAddressDTO( 1 );
    when( addressService.getAddress( any() ) ).thenReturn( a );

    given().contentType( ContentType.JSON ).body( aDto )
                      .when().post( "api/v1/addresses" ).then()
                      .contentType( ContentType.JSON )
                      .status( HttpStatus.OK ).and()
                      .body("latitude", equalTo( (float) a.getLatitude() ) )
                      .body("longitude", equalTo( (float) a.getLongitude() ) );

    verify( addressService, times( 1 ) ).getAddress( any() );
  }

  @Test
  void testPostAddressWithInvalidUser_ThenReturnBadRequest() {
    AddressDTO aDto = createAddressDTO( 1 );
    when( addressService.getAddress( any() ) ).thenReturn( null );

    given().contentType( ContentType.JSON ).body( aDto )
                      .when().post( "api/v1/addresses" ).then()
                      .status( HttpStatus.BAD_REQUEST );

    verify( addressService, times( 1 ) ).getAddress( any() );
  }

  /* helpers */

  private User createUser( long i ) {
    return new User( i, "Pedro", "safepassword", "pdfl" + i + "@ua.pt", false, null, null );
  }

  private Address createAddress( int i ) { 
    return new Address( i,  null, 40.640506, -8.653754 );
  }

  private AddressDTO createAddressDTO( int i ) {

    AddressDTO addressDto = new AddressDTO();

    addressDto.setUserId(user.getId());
    addressDto.setLatitude(40.640506);
    addressDto.setLongitude(-8.653754);

    System.out.println( "AddressDTO: " + addressDto.toString());

    return addressDto;
  }
}

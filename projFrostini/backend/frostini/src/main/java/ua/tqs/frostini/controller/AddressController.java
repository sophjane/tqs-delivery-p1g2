package ua.tqs.frostini.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ua.tqs.frostini.datamodels.AddressDTO;
import ua.tqs.frostini.service.AddressService;

@RestController
@RequestMapping("api/v1")
@Validated
@CrossOrigin
public class AddressController {

    @Autowired AddressService addressService;

    /* @GetMapping("/address")
    public ResponseEntity<Object> getAddressId(@Valid @RequestBody AddressDTO addressDTO) {
        return null;
    } */

    public ResponseEntity<Object> newAddressId(@Valid @RequestBody AddressDTO addressDTO) {
        // if response is null -> bad request
        // else -> OK with id (ResponseEntity.status( HttpStatus.OK ).body( order );)
        return null;
    }

    // get address <user id, street, city, zip code>
    // post <user id, street, city, zip code>
    
}
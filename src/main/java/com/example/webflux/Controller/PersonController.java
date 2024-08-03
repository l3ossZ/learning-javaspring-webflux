package com.example.webflux.Controller;


import com.example.webflux.Models.Person;
import com.example.webflux.Models.ResponseMessage;
import com.example.webflux.Services.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
@Log4j2
public class PersonController {
    private final PersonService personService;

    @PostMapping(value = "/get-all-person", produces = "application/stream+json")
    public Flux<ResponseEntity<?>> getAllPerson() throws Exception {
        try {
            Flux<Person> result = personService.getAllPerson();
            return result.collectList().flatMapMany(resultList -> {
                if (!resultList.isEmpty()) {
                    log.info("success");
                    return Flux.just(ResponseEntity.ok(resultList));
                } else {
                    log.error("not found");
                    ResponseMessage message = new ResponseMessage();
                    return Flux.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(message));
                }
            });
        } catch (Exception e) {
            ResponseMessage message = new ResponseMessage();
            return Flux.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message));
        }
    }

    @PostMapping(value = "/add-new-person",consumes = "application/json", produces = "application/stream+json")
    public Mono<ResponseEntity<?>> addNewPerson(@RequestBody Person person) {
        log.info("start add");
        return personService.addNewPerson(person)
                .map(ResponseEntity::ok);
    }


}

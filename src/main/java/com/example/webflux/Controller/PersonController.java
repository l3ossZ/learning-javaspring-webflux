package com.example.webflux.Controller;


import com.example.webflux.Models.KeepAliveMessage;
import com.example.webflux.Models.Person;
import com.example.webflux.Models.ResponseMessage;
import com.example.webflux.Services.PersonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

import static javax.xml.transform.OutputKeys.MEDIA_TYPE;

@RestController
@RequestMapping("/person")
@RequiredArgsConstructor
@Log4j2
public class PersonController {
    private final PersonService personService;

    @GetMapping(value = "/get-all-person", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<?> getAllPerson() {
        Flux<ServerSentEvent<Person>> personStream=personService.getAllPerson()
                .doOnNext(person -> log.info("get person: "+person))
                .doOnError(error->log.error("Error: ",error))
                .map(person -> ServerSentEvent.builder(person)
                        .id(String.valueOf(person.getId()))
                        .event("person")
                        .build());


        Flux<ServerSentEvent<KeepAliveMessage>> intervalStream=Flux.interval(Duration.ofSeconds(5))
                .map(i -> ServerSentEvent.<KeepAliveMessage>builder()
                        .id(String.valueOf(i))
                        .event("keep-alive")
                        .data(new KeepAliveMessage("ping"))
                        .build());
        return Flux.merge(personStream,intervalStream);
    }

    @PostMapping(value = "/add-new-person")
    public Mono<ResponseEntity<?>> addNewPerson(@RequestBody Person person) {
        log.info("start add");
        return personService.addNewPerson(person)
                .map(result -> {
                    log.info("add success");
                    return ResponseEntity.ok(result);
                }).retryWhen(Retry.backoff(5, Duration.ofSeconds(1))).map(ResponseEntity::ok);
    }
    @PostMapping(value = "/update-person")
    public Mono<ResponseEntity<?>> updatePerson(@RequestBody Person person) {
        return personService.updatePerson(person.getId(), person)
                .map(ResponseEntity::ok);
    }
    @PostMapping(value = "/delete-person")
    public Mono<ResponseEntity<?>> deletePerson(@RequestBody Person person) {
        return personService.deletePerson(person.getId())
                .map(ResponseEntity::ok);
    }


}

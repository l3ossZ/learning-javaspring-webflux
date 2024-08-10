package com.example.webflux.Services;

import com.example.webflux.Models.Person;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
@Log4j2
@Service
public class PersonService {
    private final Map<String, Person> personMap=new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(1);
    private final Sinks.Many<Person> personSink = Sinks.many().multicast().directBestEffort();

    public Flux<Person> getAllPerson(){
        return personSink.asFlux().mergeWith(Flux.fromIterable(personMap.values()));
    }

    public Mono<Person> addNewPerson(Person person){
        person.setId(counter.getAndIncrement());
        personMap.put(String.valueOf(person.getId()),person);
        Sinks.EmitResult result = personSink.tryEmitNext(person);
        if (result.isFailure()) {
            log.info("Failed to emit person: " + result.name());

        }else{
            log.info("Emit person: " + result.name());
        }
        return Mono.just(person);

    }

    public Mono<Person> getPersonById(int id){
        return Mono.justOrEmpty(personMap.get(String.valueOf(id)));
    }

    public Mono<Person> updatePerson(int id,Person person){
        person.setId(id);
        personMap.put(String.valueOf(id),person);
        personSink.tryEmitNext(person);
        return Mono.just(person);
    }

    public Mono<Void> deletePerson(int id) {
        Person removedPerson = personMap.remove(String.valueOf(id));
        if (removedPerson != null) {
            personSink.tryEmitNext(removedPerson); // Emit removed person to the sink
        }
        return Mono.empty();
    }
}

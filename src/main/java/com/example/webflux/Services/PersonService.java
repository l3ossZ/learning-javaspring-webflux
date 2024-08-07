package com.example.webflux.Services;

import com.example.webflux.Models.Person;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PersonService {
    private final Map<String, Person> personMap=new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(1);
    private final Sinks.Many<Person> personSink = Sinks.many().multicast().onBackpressureBuffer();

    public Flux<Person> getAllPerson(){
        return personSink.asFlux().mergeWith(Flux.fromIterable(personMap.values()));
    }

    public Mono<Person> addNewPerson(Person person){
        person.setId(counter.getAndIncrement());
        personMap.put(String.valueOf(person.getId()),person);
        personSink.tryEmitNext(person);
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

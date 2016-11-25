package com.shufflelunch.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FireBaseDao {

    @Autowired
    FirebaseDatabase db;

    public <T> Optional<T> read(String dataPoint, Class<T> t) {
        try {
            DatabaseReference databaseReference = db.getReference(dataPoint);
            CountDownLatch lock = new CountDownLatch(1);
            final List<T> results = Lists.newArrayList();
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        results.add(dataSnapshot.getValue(t));
                    }
                    lock.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    log.error("Failed to read data.", error);
                    lock.countDown();
                }
            });
            lock.await();
            if (results.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(results.get(0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<List<T>> readList(String dataPoint, Class<T> t) {
        try {
            DatabaseReference databaseReference = db.getReference(dataPoint);
            CountDownLatch lock = new CountDownLatch(1);
            final List<T> results = Lists.newArrayList();
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        dataSnapshot.getChildren().forEach(song -> results.add(song.getValue(t)));
                    }
                    lock.countDown();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    log.error("Failed to read data.", error);
                    lock.countDown();
                }
            });
            lock.await();
            if (results.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(results);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void update(String dataPoint, Map<String, Object> updateMap) {
        try {
            DatabaseReference databaseReference = db.getReference(dataPoint);
            CountDownLatch lock = new CountDownLatch(1);
            databaseReference.updateChildren(updateMap, (arg0, arg1) -> lock.countDown());
            lock.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(String dataPoint) {
        final boolean[] result = { false };
        try {
            DatabaseReference databaseReference = db.getReference(dataPoint);
            CountDownLatch lock = new CountDownLatch(1);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        dataSnapshot.getRef().removeValue((databaseError, databaseReference1) -> {
                            result[0] = true;
                            lock.countDown();
                        });
                    } else {
                        lock.countDown();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    log.error("Failed to read data.", error);
                    lock.countDown();
                }
            });
            lock.await();
            return result[0];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

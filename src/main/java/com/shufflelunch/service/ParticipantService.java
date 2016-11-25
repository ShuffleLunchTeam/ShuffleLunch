package com.shufflelunch.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shufflelunch.dao.FireBaseDao;
import com.shufflelunch.model.Participant;

@Service
public class ParticipantService {

    @Autowired
    FireBaseDao fireBaseDao;

    public Optional<List<Participant>> getAllParticipantList() {
        return fireBaseDao.readList("participants", Participant.class);
    }

    public void addParticipant(Participant participant) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> participants = mapper.convertValue(participant,
                                                               new TypeReference<Map<String, Object>>() {});
        fireBaseDao.update("participants/" + participant.getUser().getMid(), participants);
    }

    public boolean deleteParticipant(Participant participant) {
        return deleteParticipant(participant.getUser().getMid());
    }

    public boolean deleteParticipant(String userId) {
        return fireBaseDao.delete("participants/" + userId);
    }

    public boolean deleteAllParticipant() {
        return fireBaseDao.delete("participants/");
    }
}

package com.prose.gestiondestage.entity;

import com.prose.entity.Session;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SessionTests {

    @Test
    void getNextSession(){
        Session session = new Session();
        session.setSeason("Hiver");
        session.setYear("2021");

        session.getNextSeason();

        assertEquals("Été", session.getSeason());
        assertEquals("2021", session.getYear());
    }

    @Test
    void getNextSessionSeasonInvalid(){
        Session session = new Session();
        session.setSeason("Printemps");
        session.setYear("2021");

        session.getNextSeason();

        assertEquals("Printemps", session.getSeason());
        assertEquals("2021", session.getYear());
    }

    @Test
    void sessionIsPriorToAnother(){
        Session session1 = new Session();
        session1.setSeason("Automne");
        session1.setYear("2021");

        Session session2 = new Session();
        session2.setSeason("Hiver");
        session2.setYear("2022");

        assertTrue(session1.isPriorTo(session2));
    }

    @Test
    void sessionIsPriorToAnother_Not(){
        Session session1 = new Session();
        session1.setSeason("Automne");
        session1.setYear("2021");

        Session session2 = new Session();
        session2.setSeason("Hiver");
        session2.setYear("2021");

        assertFalse(session1.isPriorTo(session2));
    }
}

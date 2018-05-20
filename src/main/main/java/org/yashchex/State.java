package org.yashchex;

import java.util.Date;

public class State {
    String status;
    Date date;

    public State(String status, Date date) {
        this.status = status;
        this.date = date;
    }

    @Override
    public String toString() {
        return "State{" +
                "status='" + status + '\'' +
                ", date=" + date +
                '}';
    }

    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }
}

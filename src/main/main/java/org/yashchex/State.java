package org.yashchex;

import java.util.Date;

public class State {
    Status status;
    Date date;

    public State(String status, Date date) {
        this.status = Status.valueOf(status);
        this.date = date;
    }

    @Override
    public String toString() {
        return "State{" +
                "status='" + status + '\'' +
                ", date=" + date +
                '}';
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = Status.valueOf(status);
    }
}

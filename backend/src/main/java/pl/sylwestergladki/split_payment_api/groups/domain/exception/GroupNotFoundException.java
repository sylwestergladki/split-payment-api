package pl.sylwestergladki.split_payment_api.groups.domain.exception;

public class GroupNotFoundException extends RuntimeException {
    public GroupNotFoundException() {
        super("Group not found");
    }
}

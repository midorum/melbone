package midorum.melbone.model.dto;

import dma.validation.Validator;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class Account implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String login;
    private final String password;
    private final int order;
    private final String commentary;

    private Account(final String name, final String login, final String password, final int order, final String commentary) {
        this.name = name;
        this.login = login;
        this.password = password;
        this.order = order;
        this.commentary = commentary;
    }

    @Override
    public String toString() {
        return "Account{" +
                "name='" + name() + '\'' +
                ", commentary='" + commentary() + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Account account = (Account) o;
        return Objects.equals(name, account.name)
                && Objects.equals(login, account.login)
                && Objects.equals(password, account.password)
                && Objects.equals(commentary, account.commentary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, login, password, commentary);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class OrderComparator implements Comparator<Account> {

        @Override
        public int compare(Account o1, Account o2) {
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            return Integer.compare(o1.order, o2.order);
        }
    }

    public static class NameComparator implements Comparator<Account> {

        @Override
        public int compare(Account o1, Account o2) {
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            return o1.name.compareTo(o2.name);
        }
    }

    public String name() {
        return name;
    }

    public String login() {
        return login;
    }

    public String password() {
        return password;
    }

    public int order() {
        return order;
    }

    public Optional<String> commentary() {
        return Optional.ofNullable(commentary);
    }

    public static class Builder {
        private String name;
        private String login;
        private String password;
        private int order = Integer.MAX_VALUE;
        private String commentary;

        public Builder name(final String name) {
            this.name = name;
            return this;
        }

        public Builder login(final String login) {
            this.login = login;
            return this;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public Builder order(final int order) {
            this.order = order;
            return this;
        }

        public Builder commentary(final String commentary) {
            this.commentary = commentary;
            return this;
        }

        public Account build() {
            return new Account(
                    Validator.checkNotNull(name).andMap(String::trim).andCheckNot(String::isBlank).orThrow("Account name cannot be empty"),
                    Validator.checkNotNull(login).andMap(String::trim).andCheckNot(String::isBlank).orThrow("Account login cannot be empty"),
                    Validator.checkNotNull(password).andMap(String::trim).andCheckNot(String::isBlank).orThrow("Account password cannot be empty"),
                    order,
                    trimToNull(commentary));
        }

        private String trimToNull(final String s) {
            if (s == null) return null;
            final String trim = s.trim();
            return trim.isBlank() ? null : trim;
        }
    }

}
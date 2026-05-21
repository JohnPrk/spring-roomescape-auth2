package roomescape.domain;

import java.util.Objects;

public class Member {

    private final Long id;
    private final String email;
    private final String password;
    private final String name;
    private final Role role;

    public Member(
            Long id,
            String email,
            String password,
            String name,
            Role role
    ) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isManager() {
        return role == Role.MANAGER;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return Objects.equals(getId(), member.getId())
                && Objects.equals(getEmail(), member.getEmail())
                && Objects.equals(getPassword(), member.getPassword())
                && Objects.equals(getName(), member.getName())
                && getRole() == member.getRole();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getEmail(), getPassword(), getName(), getRole());
    }
}

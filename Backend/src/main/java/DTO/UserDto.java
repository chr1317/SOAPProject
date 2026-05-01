package DTO;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean hasAvatar;

    public UserDto() {}

    public UserDto(Long id, String firstName, String lastName, String email, boolean hasAvatar) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.hasAvatar = hasAvatar;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public boolean isHasAvatar() { return hasAvatar; }
}
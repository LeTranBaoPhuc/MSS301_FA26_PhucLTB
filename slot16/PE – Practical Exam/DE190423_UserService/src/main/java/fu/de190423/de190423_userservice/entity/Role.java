package fu.de190423.de190423_userservice.entity;

/** Vai tro cua user. Luu vao DB duoi dang chuoi (nvarchar) qua @Enumerated(EnumType.STRING). */
public enum Role {
    USER,
    CREATOR,
    ADMIN
}

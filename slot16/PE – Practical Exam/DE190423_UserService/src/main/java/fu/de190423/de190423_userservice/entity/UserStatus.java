package fu.de190423.de190423_userservice.entity;

/** Trang thai user. Luu vao DB duoi dang chuoi (nvarchar) qua @Enumerated(EnumType.STRING). */
public enum UserStatus {
    ACTIVE,
    SUSPENDED,
    DEACTIVATED
}

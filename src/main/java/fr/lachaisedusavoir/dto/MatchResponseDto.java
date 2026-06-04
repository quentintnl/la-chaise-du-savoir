package fr.lachaisedusavoir.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDto {
    private Integer id;
    private String inviteCode;
    private Boolean status;
    private String message;
}


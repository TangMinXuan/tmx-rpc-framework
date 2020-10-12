package github.tmx;

import lombok.*;

/**
 * @author: TangMinXuan
 * @created: 2020/10/12 15:02
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello {
    private Integer id;
    private String message;
}

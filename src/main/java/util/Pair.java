package util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair {
    private Integer mainIndex;
    private Integer subIndexed;
    private String selfIndex;
}

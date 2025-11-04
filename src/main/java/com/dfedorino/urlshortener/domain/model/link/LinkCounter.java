package com.dfedorino.urlshortener.domain.model.link;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkCounter {

    private long id;
    private long count;
}

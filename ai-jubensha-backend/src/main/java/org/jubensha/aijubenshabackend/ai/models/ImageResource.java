package org.jubensha.aijubenshabackend.ai.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author luobo
 * @date 2026/2/9
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 图片链接
     */
    private String imageUrl;
    /**
     * 图片描述
     */
    private String description;
}

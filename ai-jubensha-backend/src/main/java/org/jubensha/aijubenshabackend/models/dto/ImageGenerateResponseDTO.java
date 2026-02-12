package org.jubensha.aijubenshabackend.models.dto;

import lombok.Data;

/**
 * 图片生成响应数据传输对象
 */
@Data
public class ImageGenerateResponseDTO {

    private Boolean success;
    private String imageUrl;
    private String scriptName;
    private String genre;
    private String message;
    
    public static ImageGenerateResponseDTO success(String imageUrl, String scriptName, String genre) {
        ImageGenerateResponseDTO dto = new ImageGenerateResponseDTO();
        dto.setSuccess(true);
        dto.setImageUrl(imageUrl);
        dto.setScriptName(scriptName);
        dto.setGenre(genre);
        return dto;
    }
    
    public static ImageGenerateResponseDTO asyncSubmitted(String scriptName, String message) {
        ImageGenerateResponseDTO dto = new ImageGenerateResponseDTO();
        dto.setSuccess(true);
        dto.setScriptName(scriptName);
        dto.setMessage(message);
        return dto;
    }
}
package com.example.carte.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Tableau de récapitulation global")
public class RecapDashboardDTO {

    @Schema(description = "Nombre total de points signalés", example = "18")
    private long nbPoints;

    @Schema(description = "Surface totale en m²", example = "3250.5")
    private double totalSurface;

    @Schema(description = "Avancement global en pourcentage", example = "32.5")
    private double avancementPercent;

    @Schema(description = "Budget total estimé", example = "81262500")
    private double totalBudget;

    // getters & setters
}

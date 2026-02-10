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

    @Schema(description = "Nombre de problèmes terminés", example = "5")
    private long nbTermines;

    @Schema(description = "Nombre de problèmes en cours", example = "8")
    private long nbEnCours;

    @Schema(description = "Nombre de problèmes nouveaux", example = "5")
    private long nbNouveaux;

    @Schema(description = "Nombre de signalements non traités (sans problème)", example = "10")
    private long nbNonTraites;

    @Schema(description = "Nombre total de signalements", example = "18")
    private long nbSignalements;

    @Schema(description = "Délai moyen de traitement en jours", example = "12.5")
    private double delaiMoyenJours;

    // getters & setters
}

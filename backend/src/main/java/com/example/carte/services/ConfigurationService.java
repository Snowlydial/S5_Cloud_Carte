package com.example.carte.services;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.carte.entities.Configuration;
import com.example.carte.repository.ConfigurationRepository;

import java.util.List;

@Service
@Transactional
public class ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    public ConfigurationService(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

   
    public Configuration getConfiguration() {
        List<Configuration> configs = configurationRepository.findAll();

        if (configs.isEmpty()) {
            Configuration config = new Configuration();
            config.setTentativeMax(3);          // valeur par défaut
            config.setM2Forfaitaire(1000.0);    // valeur par défaut
            return configurationRepository.save(config);
        }

        return configs.get(0);
    }

    public Configuration updateConfiguration(Integer tentativeMax, Double m2Forfaitaire) {
        Configuration config = getConfiguration();

        if (tentativeMax != null) {
            config.setTentativeMax(tentativeMax);
        }
        if (m2Forfaitaire != null) {
            config.setM2Forfaitaire(m2Forfaitaire);
        }

        return configurationRepository.save(config);
    }


    public int getTentativeMax() {
        return getConfiguration().getTentativeMax();
    }


    public double getM2Forfaitaire() {
        return getConfiguration().getM2Forfaitaire();
    }

    public double calculerBudget(int niveau) {
        List<Configuration> configuration = configurationRepository.findAll();
        Configuration c = configuration.get(0);
        return c.getM2Forfaitaire() * getM2Forfaitaire() * niveau;
    }
}

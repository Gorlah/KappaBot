package com.gorlah.kappabot.subcommand.root.image;

import com.gorlah.kappabot.command.CommandPayload;
import com.gorlah.kappabot.jpa.entity.Image;
import com.gorlah.kappabot.jpa.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageAdd extends ImageSubcommand {

    private final ImageRepository imageRepository;
    private final ImageHelper imageHelper;

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getHelpText() {
        return "Adds an image to the database. Takes an image alias and URL as parameters.";
    }

    @Override
    public String process(CommandPayload payload) {
        val parameters = payload.getParameters();

        if (parameters.isEmpty()) {
            return "I need an image alias and URL to store.";
        } else if (parameters.size() == 1) {
            return "I need an image URL to store.";
        }

        String query = imageHelper.stripQuery(parameters.subList(0, parameters.size() - 1));
        Image image = imageHelper.getImage(query);

        if (image != null) {
            return "An image with that alias already exists.";
        }

        image = new Image();
        image.setAlias(query);
        image.setUrl(parameters.get(parameters.size() - 1));
        image.setUser(payload.getMetadata().getAuthor());

        image = imageRepository.save(image);

        return "Added image '" + image.getAlias() + "' to image set.";
    }
}

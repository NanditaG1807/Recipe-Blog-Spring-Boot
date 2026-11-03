package com.main.recipeblog.service;

import com.main.recipeblog.entity.Recipe;
import com.main.recipeblog.repository.RecipeRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

@Service
public class RecipePdfService {

    private final RecipeRepository recipeRepository;

    public RecipePdfService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    // सर्व Recipes एकाच PDF मध्ये तयार करणे
    public byte[] generateAllRecipesPdf() throws Exception {

        List<Recipe> recipes =
                recipeRepository.findAllByOrderByIdAsc();

        StringBuilder html = new StringBuilder();

        html.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8"/>
                <style>
                    @page {
                        size: A4;
                        margin: 20mm;
                    }

                    body {
                        font-family: Helvetica, sans-serif;
                        color: #333333;
                        font-size: 11pt;
                    }

                    h1 {
                        text-align: center;
                        color: #27765b;
                        margin-bottom: 30px;
                    }

                    h2 {
                        color: #27765b;
                        border-bottom: 1px solid #dddddd;
                        padding-bottom: 6px;
                    }

                    h3 {
                        color: #c65d35;
                    }

                    .recipe {
                        margin-bottom: 25px;
                    }

                    .recipe-image {
                        display: block;
                        width: 250px;
                        height: 180px;
                        margin: 12px 0;
                    }

                    .description, .details {
                        color: #555555;
                    }

                    .content {
                        white-space: pre-wrap;
                        line-height: 1.5;
                    }

                    .empty {
                        text-align: center;
                        color: #777777;
                    }
                </style>
            </head>
            <body>
                <h1>My Recipe Collection</h1>
            """);

        if (recipes.isEmpty()) {
            html.append(
                    "<p class='empty'>No recipes available.</p>"
            );
        }

        int count = 1;

        for (Recipe recipe : recipes) {

            String recipeName = recipe.getTitle();

            if (recipeName == null || recipeName.isBlank()) {
                recipeName = recipe.getName();
            }

            if (recipeName == null || recipeName.isBlank()) {
                recipeName = "Recipe " + count;
            }

            html.append("<div class='recipe'>");

            // Recipe title
            html.append("<h2>")
                    .append(count++)
                    .append(". ")
                    .append(escapeHtml(recipeName))
                    .append("</h2>");

            // Recipe image: Base64 data URL वापरून embed करणे
            String imageDataUrl = getImageDataUrl(recipe.getImage());

            if (!imageDataUrl.isEmpty()) {
                html.append("<img class='recipe-image' src='")
                        .append(imageDataUrl)
                        .append("' />");
            }

            // Description
            if (recipe.getDescription() != null
                    && !recipe.getDescription().isBlank()) {

                html.append("<p class='description'>")
                        .append(escapeHtml(recipe.getDescription()))
                        .append("</p>");
            }

            // Servings आणि cooking time
            html.append("<p class='details'>")
                    .append("Servings: ")
                    .append(recipe.getServings())
                    .append(" | Cooking time: ")
                    .append(recipe.getCookTime())
                    .append(" minutes")
                    .append("</p>");

            // Ingredients
            html.append("<h3>Ingredients</h3>");
            html.append("<div class='content'>")
                    .append(escapeHtml(recipe.getIngredients()))
                    .append("</div>");

            // Cooking steps
            html.append("<h3>Cooking Steps</h3>");
            html.append("<div class='content'>")
                    .append(escapeHtml(recipe.getCookingSteps()))
                    .append("</div>");

            html.append("</div>");
        }

        html.append("</body></html>");

        try (ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.useFastMode();
            builder.withHtmlContent(html.toString(), null);
            builder.toStream(output);
            builder.run();

            return output.toByteArray();
        }
    }

    // Local static folder मधून image वाचून Base64 मध्ये convert करणे
    private String getImageDataUrl(String imagePath) {

        if (imagePath == null || imagePath.isBlank()) {
            return "";
        }

        try {
            String filename = imagePath.trim()
                    .replace("\\", "/");

            // /img/image.jpg असल्यास सुरुवातीचा slash काढणे
            while (filename.startsWith("/")) {
                filename = filename.substring(1);
            }

            Path root = Path.of(
                    "src", "main", "resources", "static", "img"
            ).toAbsolutePath().normalize();

            // Database path: img/image.jpg
            // Static image root: static/img/
            String relativePath = filename;

            if (relativePath.startsWith("img/")) {
                relativePath = relativePath.substring(4);
            }

            Path imageFile = root.resolve(relativePath)
                    .normalize();

            // img folder च्या बाहेरील file वाचू नये
            if (!imageFile.startsWith(root)
                    || !Files.isRegularFile(imageFile)) {

                System.err.println(
                        "Recipe image not found: " + imageFile
                );
                return "";
            }

            String contentType =
                    Files.probeContentType(imageFile);

            if (contentType == null
                    || !contentType.startsWith("image/")) {

                System.err.println(
                        "Invalid image type: " + imageFile
                );
                return "";
            }

            byte[] imageBytes = Files.readAllBytes(imageFile);

            String base64 = Base64.getEncoder()
                    .encodeToString(imageBytes);

            return "data:" + contentType + ";base64," + base64;

        } catch (IOException | IllegalArgumentException e) {

            System.err.println(
                    "Unable to load recipe image: "
                            + e.getMessage()
            );

            return "";
        }
    }

    // Recipe बदलली आहे का हे तपासण्यासाठी version
    public String getRecipesVersion() throws Exception {

        List<Recipe> recipes =
                recipeRepository.findAllByOrderByIdAsc();

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        for (Recipe recipe : recipes) {

            updateDigest(digest, recipe.getId());
            updateDigest(digest, recipe.getTitle());
            updateDigest(digest, recipe.getName());
            updateDigest(digest, recipe.getDescription());
            updateDigest(digest, recipe.getIngredients());
            updateDigest(digest, recipe.getCookingSteps());
            updateDigest(digest, recipe.getImage());
            updateDigest(digest, recipe.getServings());
            updateDigest(digest, recipe.getCookTime());
            updateDigest(digest, recipe.getLikes());
            updateDigest(digest, recipe.getCardType());
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private void updateDigest(
            MessageDigest digest, Object value) {

        String text = value == null ? "" : value.toString();

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] length = Integer.toString(bytes.length)
                .getBytes(StandardCharsets.UTF_8);

        digest.update(length);
        digest.update((byte) ':');
        digest.update(bytes);
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
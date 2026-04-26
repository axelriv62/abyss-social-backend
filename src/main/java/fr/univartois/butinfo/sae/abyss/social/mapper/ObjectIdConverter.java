package fr.univartois.butinfo.sae.abyss.social.mapper;

import org.bson.types.ObjectId;

/**
 * Utility class for converting ObjectId to String and vice versa.
 * Reusable across all mappers.
 */
public class ObjectIdConverter {

    /**
     * Converts an ObjectId to its hexadecimal String representation
     * @param objectId the ObjectId to convert
     * @return the String representation or null if objectId is null
     */
    public static String objectIdToString(ObjectId objectId) {
        return objectId == null ? null : objectId.toHexString();
    }

    /**
     * Converts a String hexadecimal representation to an ObjectId
     * @param id the String representation of the ObjectId
     * @return the ObjectId or null if id is null or invalid
     */
    public static ObjectId stringToObjectId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

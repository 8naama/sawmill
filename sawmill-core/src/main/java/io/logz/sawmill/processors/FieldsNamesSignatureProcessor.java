package io.logz.sawmill.processors;

import io.logz.sawmill.Doc;
import io.logz.sawmill.ProcessResult;
import io.logz.sawmill.Processor;
import io.logz.sawmill.annotations.ProcessorProvider;
import io.logz.sawmill.utilities.JsonUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

@ProcessorProvider(type = "fieldsNamesSignature", factory = FieldsNamesSignatureProcessor.Factory.class)
public class FieldsNamesSignatureProcessor implements Processor {

    //RateLimitedLogger
    private final boolean includeTypeFieldInSignature;
    private final String SIGNATURE_FIELD_NAME = "logzio_fields_signature";
    public FieldsNamesSignatureProcessor(boolean includeTypeFieldInSignature) {
        this.includeTypeFieldInSignature = includeTypeFieldInSignature;
    }

    @Override
    public ProcessResult process(Doc doc) throws InterruptedException {
        try {
            Set<String> fields = extractFieldsNames(doc);
            addSignatureField(doc, createSignature(fields, doc.getField("type")));
            return ProcessResult.success();
        } catch (Exception e) {
            //use RateLimitedLogger if possible
        }
        return ProcessResult.failure(String.format("failed to add field %s to doc", SIGNATURE_FIELD_NAME));
    }

    private void addSignatureField(Doc doc, int signature) {
        doc.addField(SIGNATURE_FIELD_NAME, signature);
    }

    private int createSignature(Set<String> fields, String type) {
        return includeTypeFieldInSignature ?
                (type + JsonUtils.toJsonString(fields)).hashCode()
                : JsonUtils.toJsonString(fields).hashCode();
    }

    private Set<String> extractFieldsNames(Doc doc) {
        Set<String> fields = new HashSet<>();
        JSONObject logObject = new JSONObject(doc.getSource());
        extractFields(logObject, null, fields);
        return fields;
    }

    private void extractFields(Object obj, String key, Set<String> fields) {
        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;

            jsonObject.keySet().forEach(childKey ->
                extractFields(jsonObject.get(childKey),
                        key != null ? key + '.' + childKey : childKey,
                        fields));
        } else if (obj instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) obj;
            fields.add(key);
            IntStream.range(0, jsonArray.length())
                    .mapToObj(jsonArray::get)
                    .forEach(jsonObject -> extractFields(jsonObject, key, fields));
        } else {
            fields.add(key);
        }
    }

    public static class Factory implements Processor.Factory {
        public Factory() {}

        @Override
        public FieldsNamesSignatureProcessor create(Map<String,Object> config) {
            FieldsNamesSignatureProcessor.Configuration fieldsNamesSignatureConfig =
                    JsonUtils.fromJsonMap(FieldsNamesSignatureProcessor.Configuration.class, config);
            return new FieldsNamesSignatureProcessor(fieldsNamesSignatureConfig.getIncludeTypeFieldInSignature());
        }
    }

    public static class Configuration implements Processor.Configuration {
        private final boolean includeTypeFieldInSignature;
        public Configuration(boolean includeTypeFieldInSignature) {
            this.includeTypeFieldInSignature = includeTypeFieldInSignature;
        }

        public boolean getIncludeTypeFieldInSignature() { return includeTypeFieldInSignature; }
    }
}

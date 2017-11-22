package io.logz.sawmill.processors;

import com.google.common.base.Utf8;
import io.logz.sawmill.Doc;
import io.logz.sawmill.ProcessResult;
import io.logz.sawmill.utilities.JsonUtils;
import org.junit.Test;

import static io.logz.sawmill.utils.DocUtils.createDoc;
import static io.logz.sawmill.utils.FactoryUtils.createConfig;
import static io.logz.sawmill.utils.FactoryUtils.createProcessor;
import static org.assertj.core.api.Assertions.assertThat;

public class DocSizeProcessorTest {

    @Test
    public void testDocSizeFieldExists(){
        Doc doc = createDoc("docSize", 15);

        DocSizeProcessor sizeProcessor = createProcessor(DocSizeProcessor.class, createConfig("charset", "utf8"));
        ProcessResult processResult = sizeProcessor.process(doc);
        assertThat(processResult.isSucceeded()).isFalse();
    }

    @Test
    public void utf8CharacterSet(){
        String s = "Hello, UTF-8!";
        Doc doc = createDoc("testField", s);

        DocSizeProcessor sizeProcessor = createProcessor(DocSizeProcessor.class);
        ProcessResult processResult = sizeProcessor.process(doc);

        assertThat(processResult.isSucceeded()).isTrue();
        assertThat(doc.hasField("docSize")).isTrue();
        assertThat((int) doc.getField("docSize")).isEqualTo(Utf8.encodedLength(JsonUtils.toJsonString(createDoc("testField", s).getSource())));
    }


    @Test
    public void differentLangTest(){
        String s = "こんにちは世界!";
        Doc doc = createDoc("testField", s);

        DocSizeProcessor sizeProcessor = createProcessor(DocSizeProcessor.class);
        ProcessResult processResult = sizeProcessor.process(doc);

        assertThat(processResult.isSucceeded()).isTrue();
        assertThat(doc.hasField("docSize")).isTrue();
        assertThat((int) doc.getField("docSize")).isEqualTo(Utf8.encodedLength(JsonUtils.toJsonString(createDoc("testField", s).getSource())));
    }

}

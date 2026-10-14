package com.juans.inspeccion.data.local;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static org.junit.Assert.*;
import static com.juans.inspeccion.data.local.StorageFormatProbe.Outcome.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29, application=android.app.Application.class)
public class StorageFormatProbeTest {
    @Rule public TemporaryFolder folder=new TemporaryFolder();
    private StorageFormatProbe.Outcome json(String text){return StorageFormatProbe.inspect(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),StoredInspections.LEGACY_DRAFTS);}
    // DRAFT-006 / UPLOAD-004: collection encoding recognition, no legacy deserialization/app-version guess.
    @Test public void releasedCollectionsAreRecognizedWithoutModifyingTheirBytes() throws Exception {for(String name:new String[]{StoredInspections.LEGACY_DRAFTS,StoredInspections.UPLOADS}){File copy=folder.newFile(name);try(InputStream in=getClass().getResourceAsStream("/released-d9c52d3/"+name)){assertNotNull(in);Files.copy(in,copy.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING);}byte[] before=Files.readAllBytes(copy.toPath());assertEquals(UNSUPPORTED_OLDER,StorageFormatProbe.inspect(copy,name));assertArrayEquals(before,Files.readAllBytes(copy.toPath()));}}
    @Test public void serializationSignatureWithoutKnownStoreLocationIsUnknown(){assertEquals(UNKNOWN,StorageFormatProbe.inspect(new ByteArrayInputStream(new byte[]{(byte)0xac,(byte)0xed,0,5}),"pending-video-capture"));}
    @Test public void recognizedHeaderDoesNotClaimPayloadIntegrity(){assertEquals(UNSUPPORTED_OLDER,StorageFormatProbe.inspect(new InputStream(){int p;final int[] header={0xac,0xed,0,5};@Override public int read() throws IOException {if(p==4)throw new IOException("Payload must not be read for encoding recognition");return header[p++];}},StoredInspections.LEGACY_DRAFTS));assertEquals(UNSUPPORTED_OLDER,StorageFormatProbe.inspect(new ByteArrayInputStream(new byte[]{(byte)0xac,(byte)0xed,0,5,1,2,3}),StoredInspections.LEGACY_DRAFTS));}
    @Test public void truncatedHeaderIsCorruptRatherThanRecognizedOlder(){for(byte[] bytes:new byte[][]{{(byte)0xac},{(byte)0xac,(byte)0xed,0}})assertEquals(CORRUPT,StorageFormatProbe.inspect(new ByteArrayInputStream(bytes),StoredInspections.LEGACY_DRAFTS));}
    @Test public void malformedSignatureAndArbitraryDataAreUnknown(){assertEquals(UNKNOWN,StorageFormatProbe.inspect(new ByteArrayInputStream(new byte[]{(byte)0xac,(byte)0xed,0,4}),StoredInspections.LEGACY_DRAFTS));assertEquals(UNKNOWN,json("not a store"));assertEquals(UNKNOWN,json("{\"format\":\"another-store\",\"schemaVersion\":1,\"records\":[]}"));}
    @Test public void writerAppVersionDoesNotControlSupportedSchema(){for(String writer:new String[]{"1.0","1.10","999"})assertEquals(SUPPORTED_SCHEMA,json("{\"format\":\"inspection-drafts\",\"schemaVersion\":1,\"writerAppVersion\":\""+writer+"\",\"records\":[]}"));}
    @Test public void explicitlyRetiredAndNewerSchemasHaveDistinctOutcomes(){assertEquals(UNSUPPORTED_OLDER,json("{\"format\":\"inspection-drafts\",\"schemaVersion\":0}"));assertEquals(UNSUPPORTED_NEWER,json("{\"format\":\"inspection-drafts\",\"schemaVersion\":2}"));}
    @Test public void malformedSupportedEnvelopeIsCorrupt(){assertEquals(CORRUPT,json("{\"format\":\"inspection-drafts\",\"schemaVersion\":1,\"records\":{}}"));assertEquals(CORRUPT,json("{\"format\":\"inspection-drafts\",\"schemaVersion\":1}"));assertEquals(CORRUPT,json("{\"format\":\"inspection-drafts\",\"schemaVersion\":1,"));}
    @Test public void trailingGarbageIsCorruptRatherThanSupported(){assertEquals(CORRUPT,json("{\"format\":\"inspection-drafts\",\"schemaVersion\":1,\"records\":[]}garbage"));}
    @Test public void schemaVersionsMustBeNonnegativeIntegers(){for(String version:new String[]{"-1","1.5","\"1\"","null"})assertEquals(CORRUPT,json("{\"format\":\"inspection-drafts\",\"schemaVersion\":"+version+",\"records\":[]}"));}
    @Test public void absentFileDiffersFromExistingEmptyFile() throws Exception {assertEquals(MISSING,StorageFormatProbe.inspect(new File(folder.getRoot(),"absent"),StoredInspections.LEGACY_DRAFTS));assertEquals(CORRUPT,StorageFormatProbe.inspect(folder.newFile("empty"),StoredInspections.LEGACY_DRAFTS));}
    @Test public void readFailureAndDirectoryAreIoErrors() throws Exception {assertEquals(IO_ERROR,StorageFormatProbe.inspect(new InputStream(){@Override public int read() throws IOException {throw new IOException("test disk failure");}},StoredInspections.LEGACY_DRAFTS));assertEquals(IO_ERROR,StorageFormatProbe.inspect(folder.newFolder("not-a-file"),StoredInspections.LEGACY_DRAFTS));}
    @Test public void supportedEnvelopeRejectsInvalidRecords(){assertEquals(CORRUPT,json("{\"format\":\"inspection-drafts\",\"schemaVersion\":1,\"records\":[{\"notYetAValidatedInspection\":true}]}"));}
}

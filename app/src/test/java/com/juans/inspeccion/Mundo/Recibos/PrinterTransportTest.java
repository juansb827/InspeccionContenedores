package com.juans.inspeccion.Mundo.Recibos;

import com.juans.inspeccion.Mundo.Comando;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 31, application = android.app.Application.class)
public class PrinterTransportTest {
    private Fake transport;
    private Impresora printer;
    private boolean allowed = true;
    @Before public void setup() {
        transport = new Fake(); printer = new Impresora(transport, context -> allowed);
        printer.setMacAdress("00:11:22:33:44:55");
    }
    @Test public void denialNeverOpensTransportOrSendsCommands() throws Exception {
        allowed = false;
        assertEquals(Impresora.ERROR_AL_CONECTAR, printer.conectar(RuntimeEnvironment.getApplication()));
        assertCannotPrint(); assertEquals(0, transport.opens); assertEquals(0, transport.sent);
    }
    @Test public void failedConnectionCannotFallThroughToPrinting() throws Exception {
        transport.failOpen = true;
        assertEquals(Impresora.ERROR_AL_CONECTAR, printer.conectar(RuntimeEnvironment.getApplication()));
        assertCannotPrint(); assertEquals(0, transport.sent); assertFalse(printer.conectado());
    }
    @Test public void revocationBetweenCommandsStopsReceipt() throws Exception {
        assertEquals(Impresora.OK, printer.conectar(RuntimeEnvironment.getApplication()));
        transport.revokeAfterCommand = true;
        assertCannotPrint(); assertEquals(1, transport.sent); assertFalse(printer.conectado());
    }
    @Test public void failedCommandStopsAllSubsequentCommands() throws Exception {
        assertEquals(Impresora.OK, printer.conectar(RuntimeEnvironment.getApplication()));
        transport.failCommand = true;
        assertCannotPrint(); assertEquals(1, transport.sent); assertTrue(transport.closed);
    }
    private void assertCannotPrint() throws Exception {
        ArrayList<Comando> commands = new ArrayList<>(); commands.add(new Comando(0,"^L")); commands.add(new Comando(0,"E"));
        try { printer.enviarComandosImpresora(commands, RuntimeEnvironment.getApplication()); fail(); }
        catch (IOException expected) { }
    }
    private class Fake implements Impresora.Transport {
        int opens, sent; boolean failOpen, failCommand, revokeAfterCommand, closed;
        public boolean open(String address) { opens++; return !failOpen; }
        public boolean command(String command) { sent++; if (revokeAfterCommand) allowed = false; return !failCommand; }
        public boolean image(int x, int y, android.graphics.Bitmap image) { sent++; return true; }
        public void close() { closed = true; }
    }
}

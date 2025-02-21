/*
 * MIT License
 *
 * Copyright (c) 2021 Staatsbibliothek zu Berlin - Preußischer Kulturbesitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.staatsbibliothek.berlin.hsp.fo.discovery.testutil;

/**
 * 
 * @author Glenn Fischer {@literal <gfischer@ub.uni-leipzig.de>}
 *
 */
public class Fixtures {

  public static final String LIST_RECORD_FIXTURE =
      "{\"id\":\"KOD-31275260\",\"aufbewahrungsOrt\":\"\",\"besitzendeInstitution\":\"<besitzende Institution>\",\"signatur\":\"S 324\",\"ueberschrift\":\"<Überschrift>\",\"beschreibStoff\":\"<Beschreibstoff>\",\"entstehungsOrt\":\"<Entstehungsort>\",\"entstehungsZeit\":\"1. bis 21. Jh.\",\"beschreibungen\":[{\"id\":\"31275260\",\"entstehungsZeit\":\"1. bis 21. Jh.\",\"titel\":\"Dionysius Carthusiensis · Johannes Magdeburgensis · Passio Sancti Ignatii · Thomas a Kempis · Ps.-Hugo de Sancto Victore · Petrus de Alliaco (?) · Jacobus Carthusiensis · Ps.-Dionysius Areopagita · Anselmus Cantuariensis · Legenda Sancti Bernwardi · Walahfridus Strabo · Iso Sangallensis · Sermones in Psalmos · Johannes Trithemius · Raimundus de Sabunde · Ascetica\",\"beschreibungsFreitext\":\"S 324Dionysius Carthusiensis · Johannes Magdeburgensis · Passio Sancti Ignatii · Thomas a Kempis · Ps.-Hugo de Sancto Victore · Petrus de Alliaco (?) · Jacobus Carthusiensis · Ps.-Dionysius Areopagita · Anselmus Cantuariensis · Legenda Sancti Bernwardi · Walahfridus Strabo · Iso Sangallensis · Sermones in Psalmos · Johannes Trithemius · Raimundus de Sabunde · AsceticaPapier · I + 355 (inkl. 2 Schaltzettel) + I Blatt · 22 × 15,5 · Bursfelde (I-III, V, VII-VIII) · um 1500 (I, VII), 1470/1480 (II), 1450 (III), 1460/1470 (IV-V), 1450/1470 (VI), 1480/1500 (VIII-IX)\",\"autoren\":[\"<Autorenname 01>\",\"<Autorenname 02>\"]}]}";


  public static final String LIST_RECORD_LIST_FIXTURE = "[" + LIST_RECORD_FIXTURE + "]";

  public static final String KOD_ID = "KOD-31275260";

  public static final String LIST_RECORD_FIXTURE_BASE64 =
      "eyJpZCI6IktPRC0zMTI3NTI2MCIsImF1ZmJld2FocnVuZ3NPcnQiOiIiLCJiZXNpdHplbmRlSW5zdGl0dXRpb24iOiI8YmVzaXR6ZW5kZSBJbnN0aXR1dGlvbj4iLCJzaWduYXR1ciI6IlMgMzI0IiwidWViZXJzY2hyaWZ0IjoiPMOcYmVyc2NocmlmdD4iLCJiZXNjaHJlaWJTdG9mZiI6IjxCZXNjaHJlaWJzdG9mZj4iLCJlbnRzdGVodW5nc09ydCI6IjxFbnRzdGVodW5nc29ydD4iLCJlbnRzdGVodW5nc1plaXQiOiIxLiBiaXMgMjEuIEpoLiIsImJlc2NocmVpYnVuZ2VuIjpbeyJpZCI6IjMxMjc1MjYwIiwiZW50c3RlaHVuZ3NaZWl0IjoiMS4gYmlzIDIxLiBKaC4iLCJ0aXRlbCI6IkRpb255c2l1cyBDYXJ0aHVzaWVuc2lzIMK3IEpvaGFubmVzIE1hZ2RlYnVyZ2Vuc2lzIMK3IFBhc3NpbyBTYW5jdGkgSWduYXRpaSDCtyBUaG9tYXMgYSBLZW1waXMgwrcgUHMuLUh1Z28gZGUgU2FuY3RvIFZpY3RvcmUgwrcgUGV0cnVzIGRlIEFsbGlhY28gKD8pIMK3IEphY29idXMgQ2FydGh1c2llbnNpcyDCtyBQcy4tRGlvbnlzaXVzIEFyZW9wYWdpdGEgwrcgQW5zZWxtdXMgQ2FudHVhcmllbnNpcyDCtyBMZWdlbmRhIFNhbmN0aSBCZXJud2FyZGkgwrcgV2FsYWhmcmlkdXMgU3RyYWJvIMK3IElzbyBTYW5nYWxsZW5zaXMgwrcgU2VybW9uZXMgaW4gUHNhbG1vcyDCtyBKb2hhbm5lcyBUcml0aGVtaXVzIMK3IFJhaW11bmR1cyBkZSBTYWJ1bmRlIMK3IEFzY2V0aWNhIiwiYmVzY2hyZWlidW5nc0ZyZWl0ZXh0IjoiUyAzMjREaW9ueXNpdXMgQ2FydGh1c2llbnNpcyDCtyBKb2hhbm5lcyBNYWdkZWJ1cmdlbnNpcyDCtyBQYXNzaW8gU2FuY3RpIElnbmF0aWkgwrcgVGhvbWFzIGEgS2VtcGlzIMK3IFBzLi1IdWdvIGRlIFNhbmN0byBWaWN0b3JlIMK3IFBldHJ1cyBkZSBBbGxpYWNvICg/KSDCtyBKYWNvYnVzIENhcnRodXNpZW5zaXMgwrcgUHMuLURpb255c2l1cyBBcmVvcGFnaXRhIMK3IEFuc2VsbXVzIENhbnR1YXJpZW5zaXMgwrcgTGVnZW5kYSBTYW5jdGkgQmVybndhcmRpIMK3IFdhbGFoZnJpZHVzIFN0cmFibyDCtyBJc28gU2FuZ2FsbGVuc2lzIMK3IFNlcm1vbmVzIGluIFBzYWxtb3MgwrcgSm9oYW5uZXMgVHJpdGhlbWl1cyDCtyBSYWltdW5kdXMgZGUgU2FidW5kZSDCtyBBc2NldGljYVBhcGllciDCtyBJICsgMzU1IChpbmtsLiAyIFNjaGFsdHpldHRlbCkgKyBJIEJsYXR0IMK3IDIyIMOXIDE1LDUgwrcgQnVyc2ZlbGRlIChJLUlJSSwgViwgVklJLVZJSUkpIMK3IHVtIDE1MDAgKEksIFZJSSksIDE0NzAvMTQ4MCAoSUkpLCAxNDUwIChJSUkpLCAxNDYwLzE0NzAgKElWLVYpLCAxNDUwLzE0NzAgKFZJKSwgMTQ4MC8xNTAwIChWSUlJLUlYKSIsImF1dG9yZW4iOlsiPEF1dG9yZW5uYW1lIDAxPiIsIjxBdXRvcmVubmFtZSAwMj4iXX1dfQ==";

  public static final String HIGHLIGHTING_FIXTURE =
      "{\"KOD-123\":{\"beschreibungen.0.titel\":[\"fragment1\",\"fragment2\",\"fragment3\"]}}";
}

/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.iter;

import java.util.Iterator;
import edu.rice.cs.plt.lambda.Lambda3;

/**
 * An Iterator that applies a provided mapping lambda to each corresponding member of three 
 * source iterators.  {@link #remove()} delegates to each of the source iterators.  Assumes
 * the iterators have the same length.
 */
public class TernaryMappedIterator<T1, T2, T3, R> implements Iterator<R> {

  private final Iterator<? extends T1> _source1;
  private final Iterator<? extends T2> _source2;
  private final Iterator<? extends T3> _source3;
  private final Lambda3<? super T1, ? super T2, ? super T3, ? extends R> _map;
  
  public TernaryMappedIterator(Iterator<? extends T1> source1, Iterator<? extends T2> source2, 
                               Iterator<? extends T3> source3,
                               Lambda3<? super T1, ? super T2, ? super T3, ? extends R> map) {
    _source1 = source1;
    _source2 = source2;
    _source3 = source3;
    _map = map;
  }
  
  public boolean hasNext() { return _source1.hasNext(); }
  public R next() { return _map.value(_source1.next(), _source2.next(), _source3.next()); }
  public void remove() { _source1.remove(); _source2.remove(); _source3.remove(); }
  
  /** Call the constructor (allows the type arguments to be inferred) */
  public static <T1, T2, T3, R> TernaryMappedIterator<T1, T2, T3, R> 
    make(Iterator<? extends T1> source1, Iterator<? extends T2> source2, 
         Iterator<? extends T3> source3,
         Lambda3<? super T1, ? super T2, ? super T3, ? extends R> map) {
    return new TernaryMappedIterator<T1, T2, T3, R>(source1, source2, source3, map);
  }
  
}

package syntaxtree;
import visitor.Visitor;
import visitor.TypeVisitor;

public abstract class Type {
  public abstract void accept(Visitor v);
  public abstract Type accept(TypeVisitor v);

  @Override
   public abstract String toString();
}

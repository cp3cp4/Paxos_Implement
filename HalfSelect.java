import java.util.*;

public class HalfSelect {
    /*public static void main(String[] args) {
        ArrayList<String> a = new ArrayList<>();
        String current = "e";
        Collections.addAll(a,"a","b","c","d","e","f","g","h","i");
        System.out.println(a);

        System.out.println(new HalfSelect().overHalf(a,current));
    }*/


    public List<ContentServer> overHalf(List<ContentServer> a,ContentServer currentServer){
        Collections.shuffle(a,new Random());
        int halfNum = (a.size() - 1) / 2;
        ArrayList<ContentServer> res = new ArrayList<>();

//        System.out.println(a);
        Iterator<ContentServer> it = a.iterator();
        int count = 0;
        while (it.hasNext()){
            ContentServer cur = it.next();
            if (cur.getServerNum() == currentServer.getServerNum()){
                continue;
            }
            if (count > halfNum){
                break;
            }
            res.add(cur);
            count++;
        }

        return res;
    }
}
